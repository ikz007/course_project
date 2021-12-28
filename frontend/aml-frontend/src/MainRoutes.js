import Layout from "./layout/Layout";
import * as React from 'react';
import { useRoutes } from 'react-router-dom';
import Transactions from "./views/Transactions";
import Transaction from "./views/Transaction";
import Customers from "./views/Customers";
import Customer from "./views/Customer";
import Alerts from "./views/Alerts";
import Alert from "./views/Alert";
import Accounts from "./views/Accounts";
import Account from "./views/Account";
import Quests from "./views/Quests";
import Quest from "./views/Quest";
import Conf from "./views/Conf";
const MainRoutes = {
    path: '/',
    element: <Layout/>,
    children: [
        {
            path: '/transactions',
            element: <Transactions/>
        },
        {
            path: '/transactions/:id',
            element: <Transaction/>
        },
        {
            path: '/customers',
            element: <Customers/>
        },
        {
            path: '/customers/:id',
            element: <Customer/>
        },
        {
            path: '/alerts',
            element: <Alerts/>
        },
        {
            path: '/alerts/:id',
            element: <Alert/>
        },
        {
            path: '/accounts',
            element: <Accounts/>
        },
        {
            path: '/accounts/:id',
            element: <Account/>
        },
        {
            path: '/quests',
            element: <Quests/>
        },
        {
            path: '/quests/:id',
            element: <Quest/>
        },
        {
            path: '/confetti',
            element: <Conf/>
        }
    ]
}

export default function AppRoutes() {
    return useRoutes([MainRoutes]);
}