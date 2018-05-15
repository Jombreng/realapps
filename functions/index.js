const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

const actionTypeNewLike = "new_like"
const actionTypeNewComment = "new_comment"
const actionTypeNewPost = "new_post"
const notificationTitle = "Social App"

const postsTopic = "postsTopic"

exports.pushNotificationLikes = functions.database.ref('/post-likes/{postId}/{authorId}/{likeId}').onWrite((data,context) => {

    console.log('New like was added');

    const likeAuthorId = context.params.authorId;
    const postId = context.params.postId;

    // Get liked post.
    const getPostTask = admin.database().ref(`/posts/${postId}`).once('value');

    return getPostTask.then(post => {

        if (likeAuthorId === post.val().authorId) {
            return console.log('User liked own post');
        }

        // Get the list of device notification tokens.
        const getDeviceTokensTask = admin.database().ref(`/profiles/${post.val().authorId}/notificationTokens`).once('value');
        console.log('getDeviceTokensTask path: ', `/profiles/${post.val().authorId}/notificationTokens`)

        // Get like author.
        const getLikeAuthorProfileTask = admin.database().ref(`/profiles/${likeAuthorId}`).once('value');

        Promise.all([getDeviceTokensTask, getLikeAuthorProfileTask]).then(results => {
            const tokensSnapshot = results[0];
            const likeAuthorProfile = results[1].val();

            // Check if there are any device tokens.
            if (!tokensSnapshot.hasChildren()) {
                return console.log('There are no notification tokens to send to.');
            }

            console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
            console.log('Fetched like Author profile', likeAuthorProfile);

            // Create a notification
            const payload = {
                data : {
                    actionType: actionTypeNewLike,
                    title: notificationTitle,
                    body: `${likeAuthorProfile.username} liked your post`,
                    icon: post.val().imagePath,
                    postId: postId,

                },
            };

            // Listing all tokens.
            const tokens = Object.keys(tokensSnapshot.val());
            console.log('tokens:', tokens[0]);

            // Send notifications to all tokens.
            return admin.messaging().sendToDevice(tokens, payload).then(response => {
                        // For each message check if there was an error.
                        const tokensToRemove = [];
                response.results.forEach((result, index) => {
                    const error = result.error;
                    if (error) {
                        console.error('Failure sending notification to', tokens[index], error);
                        // Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                            error.code === 'messaging/registration-token-not-registered') {
                            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                        }
                    }
                });
                return Promise.all(tokensToRemove);
            });
        }).catch( e => {
			return console.log(e);
		});
		return false;
    });
});

exports.pushNotificationComments = functions.database.ref('/post-comments/{postId}/{commentId}').onWrite((data,context) => {

    const commentId = context.params.commentId;
    const postId = context.params.postId;
    const comment = data.after.val();

    console.log('New comment was added, id: ', postId);

    // Get the commented post .
    const getPostTask = admin.database().ref(`/posts/${postId}`).once('value');

    return getPostTask.then(post => {



        // Get the list of device notification tokens.
        const getDeviceTokensTask = admin.database().ref(`/profiles/${post.val().authorId}/notificationTokens`).once('value');
        console.log('getDeviceTokensTask path: ', `/profiles/${post.val().authorId}/notificationTokens`)

        // Get post author.
        const getCommentAuthorProfileTask = admin.database().ref(`/profiles/${comment.authorId}`).once('value');
        console.log('getCommentAuthorProfileTask path: ', `/profiles/${comment.authorId}`)

        Promise.all([getDeviceTokensTask, getCommentAuthorProfileTask]).then(results => {
            const tokensSnapshot = results[0];
            const commentAuthorProfile = results[1].val();

            if (commentAuthorProfile.id === post.val().authorId) {
                return console.log('User commented own post');
            }

            // Check if there are any device tokens.
            if (!tokensSnapshot.hasChildren()) {
                return console.log('There are no notification tokens to send to.');
            }

            console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');

            // Create a notification
            const payload = {
                data : {
                    actionType: actionTypeNewComment,
                    title: notificationTitle,
                    body: `${commentAuthorProfile.username} commented your post`,
                    icon: post.val().imagePath,
                    postId: postId,
                },
            };

            // Listing all tokens.
            const tokens = Object.keys(tokensSnapshot.val());
            console.log('tokens:', tokens[0]);

            // Send notifications to all tokens.
            return admin.messaging().sendToDevice(tokens, payload).then(response => {
                        // For each message check if there was an error.
                        const tokensToRemove = [];
                response.results.forEach((result, index) => {
                    const error = result.error;
                    if (error) {
                        console.error('Failure sending notification to', tokens[index], error);
                        // Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                            error.code === 'messaging/registration-token-not-registered') {
                            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                        }
                    }
                });
                return Promise.all(tokensToRemove);
            });
        }).catch( e => {
			return console.log(e);
		});
		return false;
    });
});

exports.pushNotificationNewPost = functions.database.ref('/posts/{postId}').onWrite((data,context) => {
    const postId = context.params.postId;

    // Only edit data when it is first created.
    if (data.before.exists()) {
        console.log('Post was changed');
        return;
    }
    // Exit when the data is deleted.
    if (!data.after.exists()) {
        console.log('Post was removed');
        return;
    }

    console.log('New post was created');

    // Get post authorID.
    const getAuthorIdTask = admin.database().ref(`/posts/${postId}/authorId`).once('value');

     return getAuthorIdTask.then(authorId => {

        console.log('post author id', authorId.val());

          // Create a notification
        const payload = {
            data : {
                actionType: actionTypeNewPost,
                postId: postId,
                authorId: authorId.val(),
            },
        };

        // Send a message to devices subscribed to the provided topic.
        return admin.messaging().sendToTopic(postsTopic, payload)
                 .then(function(response) {
                   // See the MessagingTopicResponse reference documentation for the
                   // contents of response.
                   return console.log("Successfully sent info about new post :", response);
                 })
                 .catch(function(error) {
                   return console.log("Error sending info about new post:", error);
                 });
         });

});


