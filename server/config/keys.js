module.exports = {
 db:"MainProject",
 username:"postgres",
 password:"2",
 host:"127.0.0.1",
 dialect:"postgres",
 LoggerType:'tiny',
 secretOrKey:"mysecret",
 smtpServerconfig:{
    host: "smtp.mailtrap.io",
    port: 2525,
    auth: {
        user:'',
        pass: '',
       
    
    },
     debug: true, // show debug output
     logger: true // log information in console
   
 },

 
};
