import React,{Component} from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { MakePoll } from '../../redux/action/Action';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import {Link} from 'react-router-dom';
import * as Icon from 'react-bootstrap-icons';
import Button from 'react-bootstrap/Button'
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import {NotificationContainer, NotificationManager} from 'react-notifications';

class CreatePoll extends Component {
  constructor(props){
      super(props);
      this.state = {
          question:"",
          choices:[
              {
                  option:""
              },
              {
                  option:""
              }
          ]
      };
  }
  onQuestionChange = (e) => {
      this.setState({
          [e.target.id]:e.target.value
      })
  }
  onChoiceChange=(i,e)=>{
    const option=e.target.value;
    var choices=this.state.choices;
    choices[i].option=option
    this.setState({
      choices
    })
 
  }
  onRemoveOption(i,e){
     e.preventDefault();
     
     var choices=this.state.choices;
     choices.pop(i)
     this.setState({
         choices
     })

  }
  onAddOption(e){
      e.preventDefault()
      if(this.state.choices.length< 7){
        var choices=this.state.choices;
        choices.push({option:""});
        this.setState({
            choices
        })
     }
     else{
        NotificationManager.error('Only a maximum of 7 choices are allowed', 'Error ', 5000,)
     }
      
  }
 onSubmit = (e) => {
     e.preventDefault();
      const{user}=this.props.auth;

     if(this.state.choices.length <2){
        NotificationManager.error('Please Input Atleast 2 choices', 'Error ', 5000,)
     }
    
     else{
       
        const PollData={
            question:this.state.question,
            userId:user.id,
            choices:this.state.choices
        }
        this.props.MakePoll(PollData)
       
     }
    
 }
 componentWillReceiveProps(nextProps){
if(nextProps.polls.isCreated){
    this.props.history.push("/dashboard")
}
 }
 renderChoices = () => {
    const choices= this.state.choices.map((item,i  ) => {
         return (
            <Row>
            
            <Col md={{ span: 6, offset: 3 }} >
            <div class="input-group mb-3">
    
    <input key={i}
    onChange={this.onChoiceChange.bind(this,i)}
    value={this.state.choices[i].option}
    type="text" 
    class="form-control" 
    id="choices" 
    required
   
    placeholder="Choices..."
    /> 
    <div class="input-group-append">
    <Icon.X size={40} 
    color="red"
     onClick={this.onRemoveOption.bind(this,i)}   
    />
  </div>
   
  </div>
           </Col>
           </Row>
         );
     });
     return choices
 }
 renderForm = () => {
   if (this.props.auth.isAuthenticated) {
       return(
           <Container>
            <Form  onSubmit={this.onSubmit}>
            <Form.Row>
            <Form.Group as={Col} md={{ span: 6, offset: 3 }} controlId="validationCustom01">
          <Form.Label>Poll</Form.Label>
          <Form.Control
            required
            id="question"
            value={this.state.question}
            onChange={this.onQuestionChange}
            type="text"
            placeholder="Poll..."
            
          />
          <Form.Control.Feedback>Looks good!</Form.Control.Feedback>

        </Form.Group>
       
            </Form.Row>
           
               
          <Form.Label>Choices</Form.Label>
           {this.renderChoices()}
           <Form.Row>
           <Col md={{ span: 6, offset: 3 }} >
           <Button 
            onClick={this.onAddOption.bind(this)}
    variant="light" size="sm" >
     <Icon.Plus size={10}/>Add an option
    </Button>
           </Col>
           </Form.Row>
           <Form.Row>
           <Col md={{ span: 6, offset: 3 }} >
           <Button type="submit"
    variant="primary" size="md" block>
    Create Poll <Icon.Plus size={30}/>
    </Button>
           </Col>
           </Form.Row>
          
           
            </Form>
           </Container>
       );
   } else {
       
   } 
 }

  render() {
    return (
        <div style={{ marginTop: "2rem", }} className="row">
        <Container>
        <Row>
          <div className="col s12" style={{ paddingLeft: "11.250px" }}>
      <p style={{fontColor:"black"}}>  <Link to="/dashboard" >
               <Icon.ArrowLeft size={60} color="black"/> Back to
                Dashboard
              </Link></p>
                <h4>
                  <b>Create Poll</b> 
                </h4>
                <NotificationContainer/>
              </div>
            
      </Row>
      {this.renderForm()}
        </Container>
         
      </div>
    );
  }
}
CreatePoll.propTypes = {

    CreatePoll: PropTypes.func.isRequired,
    auth:PropTypes.object.isRequired,
    errors:PropTypes.object.isRequired,
    polls:PropTypes.object.isRequired,
};
const mapStateToProps = state => ({ 
    auth:state.auth,
    errors:state.errors,
    polls:state.polls
 });
const mapDispatchToProps = { MakePoll };

export default connect(mapStateToProps, mapDispatchToProps)(CreatePoll);