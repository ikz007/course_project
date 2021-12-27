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
import Loader from 'react-loader-spinner';
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

const Transaction = (props) => {
  const [value, setValue] = React.useState(0);
  const params = useParams();
  const [transaction, setTransaction] = useState(null);
  const [exists, setExists] = useState(true);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  useEffect(() => {
    fetch('http://127.0.0.1:9000/transactions/' + params.id)
    .then(data => data.json())
    .then(trns => {
        trns.success ? setTransaction(trns.result) : setExists(false) 
        setLoading(false)
    });
    fetch('http://127.0.0.1:9000/alerts/transaction/' + params.id)
    .then(data => data.json())
    .then(alrts => alrts.success ? setAlerts(alrts.result) : console.log("Failed to retrieve") );
  },[]);

  return (
    <>
    {loading && <Loader/>}
    {exists && !loading ?
    <Box sx={{ width: '100%' }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
          <Tab label="Transaction Card" {...a11yProps(0)} />
          <Tab label="Alerts" {...a11yProps(1)} />
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
      <div>
       <Link href={'/accounts/' + transaction.OurIBAN.value} ><TextField
          required
          disabled
          id="outlined-required"
          label="OurIBAN"
          value={transaction.OurIBAN?.value}
        />
        </Link>
        <TextField
          disabled
          id="outlined-disabled"
          label="TheirIBAN"
          value={transaction.TheirIBAN.value}
        />
        <TextField
          id="outlined-password-input"
          label="Amount"
          disabled
          value={transaction.Amount}
        />
         <TextField
          id="outlined-password-input"
          label="Currency"
          disabled
          value={transaction.Currency}
        />
         <TextField
          id="outlined-password-input"
          label="BookingDateTime"
          disabled
          value={transaction.BookingDateTime}
        />
         <TextField
          id="outlined-password-input"
          label="CountryCode"
          disabled
          value={transaction.CountryCode}
        />
         <TextField
          id="outlined-password-input"
          label="DebitCredit"
          disabled
          value={transaction.DebitCredit}
        />
        <TextField
          id="outlined-password-input"
          label="TransactionCode"
          disabled
          value={transaction.TransactionCode}
        />
        <TextField
          id="outlined-password-input"
          label="Description"
          disabled
          value={transaction.Description}
        />
      </div>
      </Box>
      </TabPanel>
      <TabPanel value={value} index={1}>
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
                    <Link href={'/alerts/' + row.AlertId}>{row.AlertId}</Link>
                  </TableCell>
                  <TableCell align="right">{row.Iban.value}</TableCell>
                  <TableCell align="right">{row.TransactionReferences}</TableCell>
                  <TableCell align="right">{row.DateCreated}</TableCell>
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
export default Transaction;