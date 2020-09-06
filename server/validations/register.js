const isEmpty=require('is-empty');
const Validator= require('validator');

module.exports= ValidateRegistration=(data) => {
    const errors={}
//CONVERT EMPTY FIELDS TO EMPTY STRINGS
data.username=!isEmpty(data.username)?data.username:"";
data.email=!isEmpty(data.email)?data.email:"";
data.password=!isEmpty(data.password)?data.password:"";
data.password2=!isEmpty(data.password2)?data.password2:"";

//STEP 2 VALIDATIONS

//A. USERNAME
if (Validator.isEmpty(data.username)) {
    errors.username="UserName Is Required"
}
if (!Validator.isLength(data.username,{min:6,max:15})) {
    errors.username="Username must contain a min of 6 and max 0f 15 characters"
}
//B. EMAIL
if (Validator.isEmpty(data.email)) {
    errors.email="Your Email Is Required"
}
if (!Validator.isEmail(data.email)) {
    errors.email="Incorrect Email"
}
//PASSWORD
if (Validator.isEmpty(data.password)) {
    errors.password="Your Password Is Required"
}
if (!Validator.isLength(data.password,{min:6})) {
    errors.password="Password Too Short"
}
//PASSWORD CONFIRMATION
if (Validator.isEmpty(data.password2)) {
    errors.password2="Confirm Password Is Required"
}
if (!Validator.equals(data.password2,data.password)) {
    errors.password2="Passwords Don't Match"
}
return({
    errors,
    isValid:isEmpty(errors)
})
}