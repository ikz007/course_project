import Layout from "./layout/Layout";
import * as React from 'react';
import { useRoutes } from 'react-router-dom';
import Transactions from "./views/Transactions";
const MainRoutes = {
    path: '/',
    element: <Layout/>,
    children: [
        {
            path: '/transactions',
            element: <Transactions/>
        }
    ]
}

export default function AppRoutes() {
    return useRoutes([MainRoutes]);
}