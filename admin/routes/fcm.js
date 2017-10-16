const express = require('express');
const router = express.Router();

const axios = require('axios');

// https://console.firebase.google.com/project/mobilesw-178816/settings/cloudmessaging/?hl=ko
const SERVER_KEY = '';

router.post('/send', (req, res) => {

    let sender = req.body.sender;
    let instanceIdToken = req.body.instanceIdToken;

    axios.create({
        baseURL: '',
        timeout: 3000,
        headers: {
            'Authorization': `key=${SERVER_KEY}`,
            'Content-Type': 'application/json'
        }
    // https://firebase.google.com/docs/cloud-messaging/http-server-ref?hl=ko#table1
    }).post('https://fcm.googleapis.com/fcm/send', {
        to: instanceIdToken,
        notification: {
            title: 'PhotoPlace',
            body: `${sender}님이 댓글을 달았습니다.`
        }
    })
    .then(response => {
        console.log('response:', response);
    })
    .catch(error => {
        console.log('error:', error);
    });

    return res.json({ succeed: true });
});

export default router;