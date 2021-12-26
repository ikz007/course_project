import React, {useEffect, useState} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Link from '@mui/material/Link';
const Accounts = () => {

    const [accounts, setAccounts] = useState([]);

    useEffect( () => {
        fetch('http://127.0.0.1:9000/accounts/all')
        .then(data => data.json())
        .then(acc => acc.success ? setAccounts(acc.result) : console.log("Failed to retrieve") )
    }, []);

    return (
        <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>IBAN</TableCell>
              <TableCell align="right">BBAN</TableCell>
              <TableCell align="right">AccountType</TableCell>
              <TableCell align="right">Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {accounts.map((row) => (
              <TableRow
                key={row.AlertId}
                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
              >
                <TableCell component="th" scope="row">
                  <Link href={'accounts/' + row.IBAN.value}>{row.IBAN.value}</Link>
                </TableCell>
                <TableCell align="right">{row.BBAN}</TableCell>
                <TableCell align="right">{row.AccountType}</TableCell>
                <TableCell align="right">{row.Status}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      );
    }
export default Accounts;