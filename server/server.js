const express = require('express');
const bodyParser = require("body-parser");
const helmet = require("helmet");
const morgan = require('morgan');
const protect = require('@risingstack/protect');
const compression = require('compression');
const Sequelize  = require('sequelize');
const cors = require('cors');
const keys=require("./config/keys");
const port = process.env.PORT || 5000;
const authRoute=require("./controller/auth");
const pollRoute=require("./controller/poll");
const passport=require('passport');
const app = express();


//BODY-PARSER MIDDLEWARE(ALLOWS US TO INPUT REQUESTED DATA IN AN ENCODED AND JSON FORMAT)
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

//PROTECTION MIDDLEWARE(SQL-INJECTION)
app.use(protect.express.sqlInjection({
    body: true,
    loggerFunction: console.error
  }))

//HELMET MIDDLEWARE(XSS-FILTER, NO-SNIFF ,CSURF)
app.use(helmet());

//CORS
app.use(cors());

//MORGAN MIDDLEWARE(FOR LOGGING INFO)
app.use(morgan(keys.LoggerType))

//COMPRESSION MIDDLEWARE (FOR FASTER DATA PROCESSING AND DATA TRANSFER)
app.use(compression());

//PASSPORT JWT-AUTHENTICATION
app.use(passport.initialize());
app.use(passport.session());
// Passport config
require("./config/passport")(passport)






//TESTING YOUR SEQUELIZE ROUTES
app.use("/api/auth",authRoute)
app.use("/api/polls",pollRoute)

//DB CONNECTION(POSTGRES USING AN ORM-SEQUELIZE)
/*
const sequelize = new Sequelize(keys.db, keys.username, keys.password, {
    host: keys.host,
    dialect:  keys.dialect ,
    
    
  });
  sequelize.authenticate().then(() => {
    console.log('Connection established successfully.');
  }).catch(err => {
    console.error('Unable to connect to the database:', err);
  })
 */
//SERVER AND PORT LISTENING
app.listen(port, () => console.log(`Server running on port ${port} 🔥`));

module.exports=app;