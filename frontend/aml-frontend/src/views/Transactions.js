import React, {useEffect, useState} from 'react'
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
const Transactions = () => {

    const columns =[
        {title: 'Reference', field: 'Reference'},
        {title: 'OurIBAN', field: 'OurIBAN'},
        {title: 'TheirIBAN', field: 'TheirIBAN'}
    ]

    const [transactions, setTransactions] = useState([]);

    useEffect( () => {
        fetch('http://127.0.0.1:9000/transactions/all')
        .then(data => data.json())
        .then(trns => setTransactions(trns))
    }, []);

    return (
        <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Reference</TableCell>
                <TableCell align="right">OurIBAN</TableCell>
                <TableCell align="right">TheirIBAN</TableCell>
                <TableCell align="right">Amount</TableCell>
                <TableCell align="right">Currency</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((row) => (
                <TableRow
                  key={row.Reference}
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell component="th" scope="row">
                    {row.Reference}
                  </TableCell>
                  <TableCell align="right">{row.OurIBAN}</TableCell>
                  <TableCell align="right">{row.TheirIBAN}</TableCell>
                  <TableCell align="right">{row.Amount}</TableCell>
                  <TableCell align="right">{row.Currency}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      );
    }
export default Transactions;