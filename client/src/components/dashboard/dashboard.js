import React,{Component} from 'react';
import { connect } from 'react-redux';
import { getAllPolls, LogOutUser } from '../../redux/action/Action';
import PropTypes from 'prop-types';
import Avatar from 'react-avatar';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import { css } from 'emotion';
import ClipLoader from "react-spinners/ClipLoader";
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import Datatable from 'react-bs-datatable';
import Moment from 'react-moment';
import OverlayTrigger from 'react-bootstrap/OverlayTrigger'
import Popover from 'react-bootstrap/Popover'
import * as Icon from 'react-bootstrap-icons';


const override = css`
  display: block;
  margin: 0 auto;
 
`;
class dashboard extends Component {


componentDidMount() {
    this.props.getAllPolls();
}
//BUTTON FUNCTIONALITY
onRedirect = () => {
  this.props.history.push("./create-poll")
}
EndSession=() => {
  this.props.LogOutUser()
  window.location.href="/login";
  }
//ON row click
onRow= (id) => {
  this.props.history.push(`/vote/${id}`)
}
renderLogoutButton(){
  return(
    <Icon.BoxArrowLeft onClick={this.EndSession.bind(this)}
      color="royalblue" size={50}/>
  )
}
renderCreatePoll(){
const {user}=this.props.auth
if(user.role==="admin"){
return(
  <div style={{ marginTop: "4rem", }} className="row">
  <Container> <Row>
    <Col  md={{ span: 6, offset: 3 }}>
   
    <Button onClick={this.onRedirect}
    variant="primary" size="lg" block>
    Create Poll <Icon.Plus size={30}/>
    </Button>

    </Col>
    <Col>{this.renderLogoutButton()}</Col>
</Row></Container>
 
</div>
);
}
else{
  const popover = (
    <Popover id="popover-basic">
      <Popover.Title as="h3">Oops!!</Popover.Title>
      <Popover.Content>
        Only <strong>admins</strong> have the privilege of Creating Polls
      </Popover.Content>
    </Popover>
  );
  return(
    <div style={{ marginTop: "4rem", }} className="row">
    <Container> <Row>
      <Col  md={{ span: 6, offset: 3 }}>
      <OverlayTrigger trigger="click" placement="right" overlay={popover}>
      <Button variant="primary" size="lg" block>
      Create Poll <Icon.Lock />
      </Button>
  </OverlayTrigger>
      </Col>
      <Col>{this.renderLogoutButton()}</Col>
  </Row></Container>
   
  </div>
  );
}

}
renderTable(){
    const randomColors= ['red', 'green', 'blue','purple','orange','green','violet','pink']
    const columns=[
      {
       title: 'Id',
       prop: 'Id',
       sortable: true,
       filterable: true
       
      },
      {
        title: 'Poll',
        prop: 'Poll',
        sortable: true,
        filterable: true
       
      },
      {
        title: 'Votes',
        prop: 'Votes',
        sortable: true,
        filterable: true
        
      },
      {
       title: 'Status',
        prop: 'Active',
        sortable: true,
        filterable: true
        
      },
      {
        title: 'Author',
       prop: 'Author',
       sortable: true,
    filterable: true
      },
      {
        title: 'Avatar',
        prop: 'Avatar',
        sortable: true,
        filterable: true
      },
      {
       title: 'Posted',
        prop: 'Posted',
        sortable: true,
        filterable: true
      
      },
    ];
     const data=this.props.polls.polls.map(( polls  ) => {
      
         if(polls.active===true){
          return {
            Id:polls.id,
            Poll:polls.question,
            Votes:polls.voters.length,
            Active:<p><Icon.Dot size={50} color="green"></Icon.Dot>(Active)</p> ,
            Author:polls.author.username,
            Avatar: <Avatar  color={Avatar.getRandomColor('sitebase',randomColors)} name={polls.author.username} size="40" round={true} />,
            
            Posted: <Moment fromNow>{polls.createdAt}</Moment>

        }
         }
         else{
          return {
            Id:polls.id,
            Poll:polls.question,
            Votes:polls.voters.length,
            Active:<p><Icon.Dot size={50} color="red"></Icon.Dot>(Closed)</p>,
            Author:polls.author.username,
            Avatar: <Avatar  color={Avatar.getRandomColor('sitebase',randomColors)} name={polls.author.username} size="40" round={true} />,
            
            Posted: <Moment fromNow>{polls.createdAt}</Moment>

        }
         }
        
        
     }); 
     const customLabels = {
      first: '<<',
      last: '>>',
      prev: '<',
      next: '>',
      show: 'Display',
      entries: 'rows',
      noResults: 'There is no data to be displayed'
    };
    const classes = {
      table: 'table-striped table-hover',
      theadCol: css`
        .table-datatable__root & {
          &.sortable:hover {
            background:blue;
          }
        }
      `,
      tbodyRow: css`
        &:nth-of-type(even) {
          background: #eaeaea;
        }
      `,
      paginationOptsFormText: css`
        &:first-of-type {
          margin-right: 8px;
        }
        &:last-of-type {
          margin-left: 8px;
        }
      `
    };
      if (this.props.polls.isRetrieved) {
        return(
          <div style={{ marginTop: "4rem", }} className="container">
      
        <Datatable
       
          tableHeaders={columns}
          tableBody={data}
          rowsPerPage={5}
          rowsPerPageOption={[5, 10, 15, 20]}
         initialSort={{ prop: 'Id', isAscending: true }}
         labels={customLabels}
         classes={classes}
         onRowClick={(row) => {this.onRow(row.Id)}}
        />
     
    </div>
        );
    } else {
        return(
            <div style={{ marginTop: "10rem", }} className="row">
                  <Container>
                  <div className="sweet-loading">
      <ClipLoader
        css={override}
        size={150}
        color={"#123abc"}
        
      />
      <Row>
      <Col  md={{ span: 6, offset: 3 }}>
          <h2>Retrieving Polls..</h2>
      </Col>
      </Row>
      </div>
            </Container>
            </div>
          
          
        );
    }
   

}
  render() {
    return (
      <div>
      
      {this.renderCreatePoll()}
     
      {this.renderTable()}
     
      </div>
    );
  }
}

dashboard.propTypes = {
  LogOutUser:PropTypes.func.isRequired,
 getAllPolls:PropTypes.func.isRequired,
 polls:PropTypes.object.isRequired,
 auth: PropTypes.object.isRequired,
 errors:PropTypes.object.isRequired,



};

const mapStateToProps = state =>({
polls:state.polls,
auth:state.auth,
errors:state.errors

});
const mapDispatchToProps = { 
    getAllPolls ,LogOutUser
};

export default connect(mapStateToProps, mapDispatchToProps)(dashboard);