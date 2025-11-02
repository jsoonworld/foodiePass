import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 50 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<2000'],
    },
};

const payload = JSON.stringify({
    base64EncodedImage: 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=',
    originLanguageName: 'Korean',
    userLanguageName: 'English',
    originCurrencyName: 'South Korean won',
    userCurrencyName: 'United States Dollar',
});

const params = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export default function () {
    const url = 'http://localhost:8080/menu/reconfigure';

    const res = http.post(url, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
