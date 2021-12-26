import React, {useEffect, useState} from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Link from '@mui/material/Link';
const Quests = () => {

    const [quests, setQuests] = useState([]);

    useEffect( () => {
        fetch('http://127.0.0.1:9000/quests/all')
        .then(data => data.json())
        .then(trns => trns.success ? setQuests(trns.result) : console.log("Failed to retrieve") )
    }, []);

    return (
        <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>QuestionnaireID</TableCell>
                <TableCell align="right">CustomerID</TableCell>
                <TableCell align="right">Country</TableCell>
                <TableCell align="right">MonthlyTurnover</TableCell>
                <TableCell align="right">Active</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {quests.map((row) => (
                <TableRow
                  key={row.QuestionnaireID}
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell component="th" scope="row">
                    <Link href={'/quests/' + row.QuestionnaireID}>{row.QuestionnaireID}</Link>
                  </TableCell>
                  <TableCell align="right">
                  <Link href={'/customers/' + row.CustomerID}>{row.CustomerID}</Link>
                  </TableCell>
                  <TableCell align="right">{row.Country}</TableCell>
                  <TableCell align="right">{row.MonthlyTurnover}</TableCell>
                  <TableCell align="right">{row.Active}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      );
    }
export default Quests;