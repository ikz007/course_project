import React, {useEffect, useState} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Link from '@mui/material/Link';
const Customers = () => {

    const [customers, setCustomers] = useState([]);

    useEffect( () => {
        fetch('http://127.0.0.1:9000/customers/all')
        .then(data => data.json())
        .then(trns => trns.success ? setCustomers(trns.result) : console.log("Failed to retrieve") )
    }, []);

    return (
        <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>CustomerID</TableCell>
                <TableCell align="right">CustomerName</TableCell>
                <TableCell align="right">MonthlyIncome</TableCell>
                <TableCell align="right">Status</TableCell>
                <TableCell align="right">PEP</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {customers.map((row) => (
                <TableRow
                  key={row.CustomerID}
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell component="th" scope="row">
                    <Link href={'customers/' + row.CustomerID}>{row.CustomerID}</Link>
                  </TableCell>
                  <TableCell align="right">{row.CustomerName}</TableCell>
                  <TableCell align="right">{row.MonthlyIncome}</TableCell>
                  <TableCell align="right">{row.Status}</TableCell>
                  <TableCell align="right">{row.PEP}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      );
    }
export default Customers;