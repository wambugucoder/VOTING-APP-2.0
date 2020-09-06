const isEmpty=require('is-empty');
const Validator= require('validator');

module.exports=ValidateLogin=(data) => {
    const errors={}
//CONVERT EMPTY FIELDS TO EMPTY STRINGS
data.email=!isEmpty(data.email)?data.email:"";
data.password=!isEmpty(data.password)?data.password:"";

//STEP 2 VALIDATIONS
if (Validator.isEmpty(data.email)) {
    errors.email="Your Email Is Required"
}
if (!Validator.isEmail(data.email)) {
    errors.email="Incorrect Email"
}
//PASSWORDS
if (Validator.isEmpty(data.password)) {
    errors.password="Your Password Is Required"
}
return({
    errors,
    isValid:isEmpty(errors)
})
}