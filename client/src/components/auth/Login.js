import React, { Component } from 'react'
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { LoginUser } from '../../redux/action/Action';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import {Link} from 'react-router-dom';
import * as Icon from 'react-bootstrap-icons';
import Button from 'react-bootstrap/Button'
import  HashLoader from  "react-spinners/HashLoader";
import Col from 'react-bootstrap/Col';
import { css } from "@emotion/core";
import Form from 'react-bootstrap/Form';
import Alert from 'react-bootstrap/Alert'

const override = css`
  display: block;
  margin: 0 auto;
  
`;
class Login extends Component {
    constructor(props) {
        super(props);
        this.state = { 
            email:"",
            password:"",
            errors:{},
            displayAlert:true,
         };
    }
    onChange=(e) => {
        this.setState({
            [e.target.id]:e.target.value
        })
    }


    onSubmit=(e) => {
        e.preventDefault();
        const UserData={
            email:this.state.email,
            password:this.state.password
        }
        this.props.LoginUser(UserData)
        
    }

    componentWillReceiveProps(nextProps) {
        
         if(nextProps.errors){
               this.setState({
                   errors:nextProps.errors
               }) 
        }
        if(nextProps.auth.isAuthenticated){
          this.props.history.push("/dashboard");
      }
    }
    renderAlert(){
        if(this.state.displayAlert && this.props.auth.isRegistered){
            return(
                <Alert variant="success" onClose={() => this.setState({displayAlert:false})} dismissible>
                <Alert.Heading>Email Activation!</Alert.Heading>
                <p>
                Please Check your email and click on the verification link
                </p>
              </Alert>
            );

        }
        if(this.state.displayAlert && this.props.auth.isVerified){
          return(
            <Alert variant="success" onClose={() => this.setState({displayAlert:false})} dismissible>
            <Alert.Heading>Success!</Alert.Heading>
            <p>
           You have successfully Activated your account
           </p>
          </Alert>
        );
        }
        
        else{
            return (<div></div>);
        }
    }
    
    //Do stuff
    renderForm(){
        const{errors}=this.state;
        if(this.props.auth.isLoading && this.props.errors.isLoading) {
       return(
         <div className="sweet-loading">
         
         <HashLoader
            size={150}
           color={"#123abc"}
           css={override}
           
         />
       
         
       </div>
       );
        }
        else{
          return(
           <Form noValidate  onSubmit={this.onSubmit}>
          
           <Form.Group className="row">
           <Col  md={{ span: 6, offset: 3 }}>
           <Form.Label>Email</Form.Label>
             <Form.Control 
             type="email" 
             id="email"
             isInvalid={!!errors.email}
             value={this.state.email}
             onChange={this.onChange}
             placeholder="Enter a valid email" 
             required 
             />
             <Form.Text className="text-muted">
               We'll never share your email with anyone else.
             </Form.Text>
             <Form.Control.Feedback type="invalid" tooltip>
                           {errors.email}
                         </Form.Control.Feedback>
           </Col>
             
           </Form.Group>
           <Form.Group className="row">
           <Col  md={{ span: 6, offset: 3 }}>
           <Form.Label>Password</Form.Label>
             <Form.Control 
             type="password" 
             id="password"
             isInvalid={!!errors.password}
             value={this.state.password}
             onChange={this.onChange}
             placeholder="*****" 
             required 
             />
             <Form.Control.Feedback type="invalid" tooltip>
                           {errors.password}
                         </Form.Control.Feedback>
         
           </Col>
             
           </Form.Group>
           
          
           <Button variant="primary" size="lg" type="submit">
             Submit
           </Button>
         </Form>
          )
        }

    }
    
    
    render() {
        return (
            <div style={{ marginTop: "2rem", }} className="row">
      <Container>
      {this.renderAlert()}
      <Row>
                  
      
         
      <div className="col s12" style={{ paddingLeft: "11.250px" }}>
      <p style={{fontColor:"black"}}>  <Link to="/" >
               <Icon.ArrowLeft size={60} color="black"/> Back to
                home
              </Link></p>
                <h4>
                  <b>Login</b> below
                </h4>
                <p className="grey-text text-darken-1">
                  Dont have an account? <Link to="/register">Register</Link>
                </p>
              </div>
            
      </Row>
         <Container>
         {this.renderForm()}
      </Container>
      
      </Container>

     </div>
    
        );
    }
}
Login.propTypes = {
    LoginUser:PropTypes.func.isRequired,
    auth:PropTypes.object.isRequired,
    errors:PropTypes.object.isRequired,
}

const mapStateToProps = state =>({ 
auth:state.auth,
errors:state.errors
 });

export default connect(mapStateToProps,
     {LoginUser} )
     (Login)
     