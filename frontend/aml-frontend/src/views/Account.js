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

const Account = (props) => {
  const [value, setValue] = React.useState(0);
  const params = useParams();
  const [account, setAccount] = useState(null);
  const [exists, setExists] = useState(true);
  const [alerts, setAlerts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  useEffect(() => {
    fetch('http://127.0.0.1:9000/accounts/' + params.id)
    .then(data => data.json())
    .then(acc => {
        acc.success ? setAccount(acc.result) : setExists(false) 
        setLoading(false)
    });
    fetch('http://127.0.0.1:9000/alerts/account/' + params.id)
    .then(data => data.json())
    .then(alrts => alrts.success ? setAlerts(alrts.result) : console.log("Failed to retrieve") );
    fetch('http://127.0.0.1:9000/relationships/account/' + params.id)
    .then(data => data.json())
    .then(cust => cust.success ? setCustomers(cust.result) : console.log("Failed to retrieve") );
  },[]);

  return (
    <>
    {loading && <Loader/>}
    {exists && !loading ?
    <Box sx={{ width: '100%' }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
          <Tab label="Account Card" {...a11yProps(0)} />
          <Tab label="Alerts" {...a11yProps(1)} />
          <Tab label="Customers" {...a11yProps(2)} />
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
          label="IBAN"
          value={account.IBAN?.value}
        />
        <TextField
          disabled
          id="outlined-disabled"
          label="BBAN"
          defaultValue={account.BBAN}
        />
        <TextField
          id="outlined-password-input"
          label="AccountType"
          disabled
          value={account.AccountType}
        />
         <TextField
          id="outlined-password-input"
          label="Status"
          disabled
          value={account.Status}
        />
         <TextField
          id="outlined-password-input"
          label="OpenDate"
          disabled
          value={account.OpenDate}
        />
         <TextField
          id="outlined-password-input"
          label="CloseDate"
          disabled
          value={account.CloseDate}
        />
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
                  <TableCell align="right"><Link href={'/accounts/' + row.Iban.value}>{row.Iban.value}</Link></TableCell>
                  <TableCell align="right">{row.TransactionReferences}</TableCell>
                  <TableCell align="right">{row.DateCreated}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </TabPanel>
      <TabPanel value={value} index={2}>
      <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>CustomerID</TableCell>
                <TableCell align="right">Customer Name</TableCell>
                <TableCell align="right">Business Type</TableCell>
                <TableCell align="right">Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {customers.map((row) => (
                <TableRow
                  key={row.CustomerID}
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell component="th" scope="row">
                    <Link href={'/customers/' + row.CustomerID}>{row.CustomerID}</Link>
                  </TableCell>
                  <TableCell align="right">{row.CustomerName}</TableCell>
                  <TableCell align="right">{row.BusinessType}</TableCell>
                  <TableCell align="right">{row.Status}</TableCell>
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
export default Account;