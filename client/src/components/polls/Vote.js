import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { VotePoll, GetSpecificPoll, DeletePoll, ClosePoll } from '../../redux/action/Action';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import * as Icon from 'react-bootstrap-icons';
import Button from 'react-bootstrap/Button'
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import {Link} from 'react-router-dom';
import {NotificationContainer, NotificationManager} from 'react-notifications';
import CanvasJSReact from "../../Assets/canvasjs.react";
import 'react-confirm-alert/src/react-confirm-alert.css'; 
import { confirmAlert } from 'react-confirm-alert';
var CanvasJS = CanvasJSReact.CanvasJS;
var CanvasJSChart = CanvasJSReact.CanvasJSChart;

class Vote extends React.Component {
 constructor(props){
     super(props);
     this.state = {
         choices:"",
         switch:true,
     };
 }
 onChoiceChange(e){
     this.setState({
         choices:e.target.value
     })
 }

 componentDidMount(){ 
  this.props.GetSpecificPoll(this.props.match.params.id)
 }
componentWillReceiveProps(nextProps) {
    if (nextProps.polls.hasVoted || nextProps.polls.isDeleted) {
        window.location.href = '/dashboard';
    }
    if(nextProps.polls.polls.active===false){
         this.setState({
             switch:false
         })
    }
}
onDelete = (id) => {
   this.props.DeletePoll(id) 
}
onClose(id){
    this.props.ClosePoll(id)
}
onConfirm = () => {
    confirmAlert({
        title: 'Confirm to Close',
        message: 'Once Confirmed,Voting will Stop.This process cannot be undone Are you sure to do this?',
        buttons: [
          {
            label: 'Yes',
            onClick: () =>{
                this.onClose(this.props.match.params.id)
             alert('Voting Has Been Stopped')
            } 
          },
          {
            label: 'No',
            onClick: () => alert('Cancelled Operation')
          }
        ]
      });
  };
onSubmit(e){
e.preventDefault();
const{user}=this.props.auth
 if(!this.state.choices){
    NotificationManager.error('Please input an Option', 'Error ', 5000,)
 }
 else{
     
    this.props.VotePoll(
        this.props.match.params.id,
        user.id,
        this.state.choices
        )
 }

}
renderAuthorButtons = () => {
    const {polls}=this.props.polls
    const {user}=this.props.auth
    if(polls.userId===user.id) {
      return(
        <Row>
        <Col>
        {polls.active===true||this.state.switch===true?
         <Button  onClick={this.onConfirm.bind(this)}
variant="info">Close Poll</Button>
                : 
<Button  
variant="info" disabled>Close Poll</Button>
        
}

</Col>
<Col >
<Button onClick={this.onDelete.bind(this,polls.id)}
variant="danger">Delete Poll</Button></Col>

</Row>
      );
    } else {
        return(
            <div></div>
        );
    }
}
renderButton(){
    const{polls}=this.props.polls
    const{user}=this.props.auth
    if (polls.voters.includes(`${user.id}`) || polls.active===false) {
    return(
       <div></div>
    );
} else {
    return(
         <Button  className="voting"
        variant="primary" type="submit">
          Vote
        </Button>
    );
}
}
renderOptions(){
  const{polls}=this.props.polls
  const choices= polls.choices.map((choices,i ) => {
      return(
         <option key={i} value={choices.id}>{choices.option}</option>
  
      );
 });
  return choices
}

renderResults(){
    const{polls}=this.props.polls
    const choices= polls.choices.map((choices,i ) => {
        return({
            label:choices.option,
            y:choices.votes
        });
   });
 
  
    const options = {

        title: {
            text: "Live Poll Results"
        },
        data: [
        {
            // Change type to "doughnut", "line", "splineArea", etc.
            type: "column",
            dataPoints:choices
        }
        ],
        exportEnabled: true,
        animationEnabled: true,
    }
    return (
    <div>
        <CanvasJSChart options = {options}
            /* onRef={ref => this.chart = ref} */
        />
        {/*You can get reference to the chart instance as shown above using onRef. This allows you to access all chart properties and methods*/}
    </div>
    );
}
renderOptionsOrResults(){

    const{polls}=this.props.polls
    const{user}=this.props.auth
    if (polls.voters.includes(`${user.id}`) || polls.active===false) {
        return(
            <div> {this.renderResults()}</div>
            
             );
       
    } else {
       return(
        <div class="input-group mb-3">
        <div class="input-group-prepend">
          <label class="input-group-text" for="inputGroupSelect01">Options</label>
        </div>
        <select class="custom-select" id="inputGroupSelect01" onChange={ this.onChoiceChange.bind(this)}>
          <option value="" selected>Choose...</option>
         {this.renderOptions()}
        </select>
      </div>
           ) 
    }
}
renderVotingForm(){
    const {polls}=this.props.polls
   if (this.props.polls.isRetrievedById) {
       return(
<Form  onSubmit={this.onSubmit.bind(this)}>
  <Form.Group controlId="formBasicPoll">
  <Form.Row>
     <Col md={{ span: 6, offset: 3 }} >
           <Form.Label>
        <b style={{fontFamily:'Roboto',fontSize:30}}>{polls.question}</b>
    </Form.Label>
           </Col>
           </Form.Row>
   
   
  </Form.Group>
  <Form.Row>
     <Col md={{ span: 6, offset: 3 }} >
     {this.renderOptionsOrResults()}
           </Col>
           </Form.Row>
   
 
 {this.renderButton()}
</Form>
       );
   } else {
         return(
           <div>Loading...</div>
       );
   }
}
renderTitle(){
    const{polls}=this.props.polls
    const{user}=this.props.auth
    if (polls.voters.includes(`${user.id}`) || polls.active===false) {
        return(
            <h4>
            <b>Results</b> below
       </h4> 
        );
    }
    else{
        return(
            <h4>
                 <b>Vote</b> below
            </h4>
           
        );
    }
}
  render() {
    return (
        <div style={{ marginTop: "2rem", }} className="row">
        <Container>
       
        <Row>
                    
        
           
        <div className="col s12" style={{ paddingLeft: "11.250px" }}>
        <p style={{fontColor:"black"}}>  <a href="/dashboard" >
                 <Icon.ArrowLeft size={60} color="black"/> Back to
                 Dashboard
                </a></p>
               
                {this.props.polls.isRetrievedById? this.renderAuthorButtons()  : ''}
               {this.props.polls.isRetrievedById? this.renderTitle()  : ''}
                  
                
                  
         
                  <NotificationContainer/>
                </div>
              
        </Row>
           <Container>
          {this.renderVotingForm()}
        </Container>
        
        </Container>
  
       </div>
    );
  }
}


Vote.propTypes = {
GetSpecificPoll:PropTypes.func.isRequired,
VotePoll:PropTypes.func.isRequired,
auth:PropTypes.object.isRequired,
errors:PropTypes.object.isRequired,
polls:PropTypes.object.isRequired,
}

const mapStateToProps = state=> ({
auth:state.auth,
errors:state.errors,
polls:state.polls

});

const mapDispatchToProps = {VotePoll,GetSpecificPoll,DeletePoll ,ClosePoll};

export default connect(mapStateToProps, mapDispatchToProps)(Vote);