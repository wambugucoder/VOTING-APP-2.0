import React,{Component} from 'react';
import { connect } from 'react-redux';
import {VerifyUser} from '../../redux/action/Action'
import PropTypes from 'prop-types';
import TextLoop from "react-text-loop";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import { css } from "@emotion/core";
import ClipLoader from "react-spinners/ClipLoader";
import Col from 'react-bootstrap/Col';
import Alert from 'react-bootstrap/Alert'
import {Link} from 'react-router-dom';
import Card from 'react-bootstrap/Card'

const override = css`
  display: block;
  margin: 0 auto;
 
`;
class Verify extends Component {
  constructor(props) {
    super(props);
   this.state={
     errors:{},
     displayAlert:true
    
   }

  }
 
  componentWillReceiveProps(nextProps) {
    if(nextProps.auth.isVerified){
      this.props.history.push("/login")
    }
    if(nextProps.errors){
      this.setState({errors:nextProps.errors})
    }
  }
 
componentDidMount() {
  setTimeout(() => {
    this.props.VerifyUser(this.props.match.params.token)
  }, 11000)
}
renderAlert(){

    return(
         <Alert variant="danger">
      <Alert.Heading>Oops!</Alert.Heading>
      <p>
       It seems your token Expired or has an error ,Please <Link to="/register"> Register</Link> with us again and activate your email within 1 hr
      </p>
    </Alert>
     
     
    );
  }
 

renderSpinner(){
  if(!this.props.auth.isVerified  && !this.state.errors.token ){
    return(
      <div className="sweet-loading">
      <ClipLoader
        css={override}
        size={150}
        color={"#123abc"}
        
      />
    </div>
    );
  }
  else  {
    return(
      <div></div>
    );
    }


}
renderTextLoop(){
  if(!this.props.auth.isVerified && !this.state.errors.token ){
    return (
      <TextLoop 
      interval={5000}
      fade={true}
      >
      <div>
          <span><p style={{fontFamily:"Roboto",fontSize:24}}>Verifying Token...</p></span>
         
      </div>
      <div><p style={{fontFamily:"Roboto",fontSize:24}}>Confirming Account...</p></div>
      <div><p style={{fontFamily:"Roboto",fontSize:24}}>Finalizing...</p></div>
  </TextLoop>
    );
  }
  else{
    return(
      <div>{this.renderAlert()}</div>
    );
    }

}
  render() {
    return (
      
      <div style={{ marginTop: "10rem", }} className="row">

            <Container>
            <Card >
            <Card.Header><b>ACCOUNT VERIFICATION</b></Card.Header>
              <Row>
              <Col  md={{ span: 6, offset: 3 }}>   {this.renderSpinner()}</Col>
             
              </Row>
              <Row>
              <Col  md={{ span: 6, offset: 3 }}> {this.renderTextLoop()}</Col>
               
              </Row>
              </Card>
           </Container>
   </div>
   
    );
  }
}
Verify.propTypes = {
 VerifyUser:PropTypes.func.isRequired,
 auth:PropTypes.object.isRequired,
 errors:PropTypes.object.isRequired,
}

const mapStateToProps = state => ({
 auth:state.auth,
 errors:state.errors
});

const mapDispatchToProps = {VerifyUser };

export default connect(mapStateToProps, mapDispatchToProps)(Verify);