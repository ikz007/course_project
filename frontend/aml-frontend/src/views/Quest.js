import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useParams } from "react-router";
import NotFound from './NotFound';
import TextField from '@mui/material/TextField';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Link from '@mui/material/Link';
function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          <Typography component={'span'}>{children}</Typography>
        </Box>
      )}
    </div>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.number.isRequired,
  value: PropTypes.number.isRequired,
};

function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
}

const Quest = (props) => {
  const [value, setValue] = React.useState(0);
  const params = useParams();
  const [transactions, setTransactions] = useState([]);
  const [exists, setExists] = useState(true);
  const [quest, setQuest] = useState(null);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  useEffect(() => {
    fetch('http://127.0.0.1:9000/quests/' + params.id)
    .then(data => data.json())
    .then(que => que.success ? setQuest(que.result) : setExists(false) );
    fetch('http://127.0.0.1:9000/transactions/quest/' + params.id)
    .then(data => data.json())
    .then(trns => trns.success ? setTransactions(trns.result) : console.log("Failed to retrieve") );
  },[]);

  return (
    <>
    {exists && quest != null ?
    <Box sx={{ width: '100%' }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
          <Tab label="Quest Card" {...a11yProps(0)} />
          <Tab label="Related Transactions" {...a11yProps(1)} />
        </Tabs>
      </Box>
      <TabPanel value={value} index={0}>
      <Box
      component="form"
      sx={{
        '& .MuiTextField-root': { m: 1, width: '25ch' },
      }}
      noValidate
    >
        <TextField
          required
          disabled
          id="outlined-required"
          label="QuestionnaireID"
          value={quest.QuestionnaireID}
        />
        <Link href={'/customers/' + quest.CustomerID}>
         <TextField
          id="outlined-password-input"
          label="CustomerID"
          disabled
          value={quest.CustomerID}
        />
        </Link>
        <TextField
          disabled
          id="outlined-disabled"
          label="Country"
          defaultValue={quest.Country}
        />
        <TextField
          id="outlined-password-input"
          label="MonthlyTurnover"
          disabled
          value={quest.MonthlyTurnover}
        />
         <TextField
          id="outlined-password-input"
          label="AnnualTurnover"
          disabled
          value={quest.AnnualTurnover}
        />
         <TextField
          id="outlined-password-input"
          label="Reason"
          disabled
          multiline
          rows={4}
          value={quest.Reason}
        />
         <TextField
          id="outlined-password-input"
          label="Active"
          disabled
          value={quest.Active}
        />
      </Box>
      </TabPanel>
      <TabPanel value={value} index={1}>
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
                    <Link href={'/transactions/' + row.Reference}>{row.Reference}</Link>
                  </TableCell>
                  <TableCell align="right">{row.OurIBAN.value}</TableCell>
                  <TableCell align="right">{row.TheirIBAN.value}</TableCell>
                  <TableCell align="right">{row.Amount}</TableCell>
                  <TableCell align="right">{row.Currency}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </TabPanel>
    </Box> : <NotFound/>
    }
    </>
  );
}
export default Quest;