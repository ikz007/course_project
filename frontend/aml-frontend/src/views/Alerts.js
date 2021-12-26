import React, {useEffect, useState} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Link from '@mui/material/Link';
const Alerts = () => {

    const [alerts, setAlerts] = useState([]);

    useEffect( () => {
        fetch('http://127.0.0.1:9000/alerts/all')
        .then(data => data.json())
        .then(alrts => alrts.success ? setAlerts(alrts.result) : console.log("Failed to retrieve") )
    }, []);

    return (
        <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>AlertId</TableCell>
              <TableCell align="right">IBAN</TableCell>
              <TableCell align="right">TransactionReference</TableCell>
              <TableCell align="right">DateCreated</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {alerts.map((row) => (
              <TableRow
                key={row.AlertId}
                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
              >
                <TableCell component="th" scope="row">
                  <Link href={'alerts/' + row.AlertId}>{row.AlertId}</Link>
                </TableCell>
                <TableCell align="right">{row.Iban.value}</TableCell>
                <TableCell align="right">{row.TransactionReferences}</TableCell>
                <TableCell align="right">{row.DateCreated}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      );
    }
export default Alerts;