import React, { Component } from 'react';
import { RegisterUser } from  '../../redux/action/Action' ;
import PropTypes from 'prop-types';
import Form from 'react-bootstrap/Form';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import { connect } from "react-redux";
import {Link,withRouter} from 'react-router-dom';
import * as Icon from 'react-bootstrap-icons';
import Button from 'react-bootstrap/Button'
import  HashLoader from  "react-spinners/HashLoader";
import Col from 'react-bootstrap/Col';
import { css } from "@emotion/core";


const override = css`
  display: block;
  margin: 0 auto;
  
`;
class Register extends Component {
  constructor(props) {
    super(props);
    this.state = { 
      username:"",
      email:"",
      password:"",
      password2:"",
      errors:{},
      
     };
  }

  componentWillReceiveProps(nextProps) {
    if(nextProps.errors){
      this.setState({
        errors:nextProps.errors
      })
    }
    if(nextProps.auth.isRegistered){
    
   
      this.props.history.push("/login")
    
     
    }
  }


onChange=(e) => {
  this.setState({
    [e.target.id]: e.target.value
  })
}
onSubmit=(e) => {
  e.preventDefault();
  const UserData={
    username:this.state.username,
    email:this.state.email,
    password:this.state.password,
    password2:this.state.password2
  }
  console.log(UserData)
  this.props.RegisterUser(UserData)
}
renderRegistration(){
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
    <Form.Label>Username</Form.Label>
      <Form.Control 
  
      type="text" 
      isInvalid={!!errors.username}
      id="username"
      value={this.state.username}
      onChange={this.onChange}
      placeholder="Enter Username" 
  
      required
      />
     <Form.Control.Feedback type="invalid" tooltip>
                    {errors.username}
                  </Form.Control.Feedback>
      </Col>
    </Form.Group>
    
    
  
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
    <Form.Group className="row">
    <Col  md={{ span: 6, offset: 3 }}>
    <Form.Label>Confirm Password</Form.Label>
      <Form.Control 
      type="password" 
      id="password2"
      isInvalid={!!errors.password2}
      value={this.state.password2}
      onChange={this.onChange}
      placeholder="****"
      required  />
    <Form.Control.Feedback type="invalid" tooltip>
                    {errors.password2}
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
      <Row>
                  
      
         
      <div className="col s12" style={{ paddingLeft: "11.250px" }}>
      <p style={{fontColor:"black"}}>  <Link to="/" >
               <Icon.ArrowLeft size={60} color="black"/> Back to
                home
              </Link></p>
                <h4>
                  <b>Register</b> below
                </h4>
                <p className="grey-text text-darken-1">
                  Already have an account? <Link to="/login">Log in</Link>
                </p>
              </div>
              
      </Row>
         <Container>
         {this.renderRegistration()}
      </Container>
      
      </Container>

     </div>
    
      
    );
  }
}


Register.propTypes = {
  RegisterUser:PropTypes.func.isRequired,
  auth:PropTypes.object.isRequired,
  errors:PropTypes.object.isRequired,
};

const mapStateToProps = state => ({ 
auth:state.auth,
errors:state.errors
    
});
export default connect(mapStateToProps,
 {RegisterUser})
 (withRouter(Register));

