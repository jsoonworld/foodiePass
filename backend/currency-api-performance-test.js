import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',

    thresholds: {
        'http_req_duration': ['p(95)<200'],
    },
};

const API_BASE_URL = 'http://localhost:8080';

export default function () {
    const url = `${API_BASE_URL}/currency/calculate`;

    const payload = JSON.stringify({
        originCurrency: "South Korean won",
        userCurrency: "Japanese Yen",
        orders: [
            { originPrice: 10000, quantity: 2 },
            { originPrice: 5000, quantity: 5 },
        ],
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
