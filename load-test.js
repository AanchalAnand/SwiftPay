import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 250,
    duration: '1m',
};

export default function () {

    const payload = JSON.stringify({
        senderId: "1",
        receiverId: "2",
        amount: 100,
        currency: "INR",
        idempotencyKey: `${__VU}-${__ITER}`
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    http.post(
        'http://localhost:8080/v1/payments',
        payload,
        params
    );

    sleep(1);
}