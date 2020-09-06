import React,{Component} from 'react';

import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';

class NavBarMain extends Component {
    
    
    render() {
        return (
            <Navbar fixed="top"
            collapseOnSelect expand="lg" bg="dark" variant="dark">
  <Navbar.Brand href="/">POLLING APP</Navbar.Brand>
  <Navbar.Toggle aria-controls="responsive-navbar-nav" />
  <Navbar.Collapse id="responsive-navbar-nav">
   
     <Nav className="mr-auto">
      <Nav.Link href="/login">Login</Nav.Link>
      <Nav.Link  href="/register">Register</Nav.Link>
    </Nav>
  </Navbar.Collapse>
</Navbar>
        );
    }
}
export default NavBarMain