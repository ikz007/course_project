import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useParams } from "react-router";
import NotFound from './NotFound';
import TextField from '@mui/material/TextField';
import Loader from 'react-loader-spinner';
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

const Alert = (props) => {
  const [value, setValue] = React.useState(0);
  const params = useParams();
  const [exists, setExists] = useState(true);
  const [alert, setAlert] = useState(null);
  const [loading, setLoading] = useState(true);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  useEffect(() => {
    fetch('http://127.0.0.1:9000/alerts/' + params.id)
    .then(data => data.json())
    .then(cust => {
        cust.success ? setAlert(cust.result) : setExists(false)
        setLoading(false)
    } );
  },[]);

  return (
    <>
    {loading && <Loader/>}
    {exists && !loading ?
    <Box sx={{ width: '100%' }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
          <Tab label="Alert Card" {...a11yProps(0)} />
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
        <TextField
          required
          disabled
          id="outlined-required"
          label="AlertId"
          value={alert.AlertId}
        />
        <TextField
          disabled
          id="outlined-disabled"
          label="DateCreated"
          defaultValue={alert.DateCreated}
        />
        <Link href={'/transactions/'+ alert.TransactionReferences}>
        <TextField
          id="outlined-password-input"
          label="TransactionReferences"
          disabled
          value={alert.TransactionReferences}
        />
        </Link>
        <Link href={'/accounts/' + alert.Iban.value}>
         <TextField
          id="outlined-password-input"
          label="IBAN"
          disabled
          value={alert.Iban.value}
        />
        </Link>
         <TextField
          id="outlined-password-input"
          label="AlertedCondition"
          multiline
          rows={4}
          disabled
          value={alert.AlertedCondition}
        />
      </div>
      </Box>
      </TabPanel>
    </Box> : <NotFound/>
    }
    </>
  );
}
export default Alert;