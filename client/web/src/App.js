import React from 'react';
import {Reactor} from './Reactor';
import {HashRouter, Link, Route} from 'react-router-dom'
import {Crud} from "./Crud";
import {Icon} from './Widgets';

const hexDigits  = '0123456789abcdef';

const dec2hex = dec => {
  var basis = 16;
  var remainder = dec;
  var accumulatedDigits = [];
  do {
    const x = remainder % basis;
    const digit=  hexDigits[x];
    accumulatedDigits.push(digit);
    remainder -= x;
    remainder /= 16;
  } while (0 < remainder);
  return accumulatedDigits77.reduce((hexString, digit) => hexString + digit, '0x');
};

console.log(dec2hex(0));
console.log(dec2hex(10));
console.log(dec2hex(20));
console.log(dec2hex(42));
function NavBar(props) {
  void props;
  return <nav className="navbar navbar-expand-md navbar-dark fixed-top bg-dark">
    <Link className="navbar-brand" to="#">Navbar</Link>
    <button className="navbar-toggler" type="button" data-toggle="collapse"
            data-target="#navbarsExampleDefault"
            aria-controls="navbarsExampleDefault" aria-expanded="false" aria-label="Toggle navigation">
      <span className="navbar-toggler-icon"/>
    </button>
    <div className="collapse navbar-collapse" id="navbarsExampleDefault">
      <ul className="navbar-nav mr-auto">
        <li className="nav-item active">
          <Link className="nav-link" to="/">Home<span
              className="sr-only">(current)</span></Link>
        </li>
        <li className="nav-item">
          <Link className="nav-link" to='/crud'>Crud</Link>
        </li>
        <li className="nav-item">
          <Link className="nav-link" to='/about'>About</Link>
        </li>
        <li className="nav-item dropdown">
          <Link className="nav-link dropdown-toggle disabled" to="" id="dropdown01"
                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Games</Link>
          <div className="dropdown-menu" aria-labelledby="dropdown01">
            <Link className="dropdown-item" to="#">Ken Ken</Link>
            <Link className="dropdown-item" to="#">Mancala</Link>
            <Link className="dropdown-item" to="#">Spades</Link>
          </div>
        </li>
      </ul>
    </div>
  </nav>
}
function Home(props) {
  void props;
  return <div>Home</div>;
}
function About(props) {
  void props;
  return <h3>about...</h3>
}
class App extends Reactor {
  constructor(props) {
    super(props);
    super.lenses({activePage: null});
  }
  render() {
    return (
        <HashRouter basename="/">
          <div>
            <NavBar routes={this.routes}/>
            <main role="main">
              <div className="jumbotron">
                <div className="container">
                  <h1 className="display-3">
                    <Icon name='aperture' size={{height: 80, width: 80}}/> InterScalactic
                  </h1>
                  <p>Demonstration of React, Akka, Slick.</p>
                </div>
              </div>
              <div className="container">
                <Route exact path='/' component={Home}/>
                <Route path='/crud' component={Crud}/>
                <Route path='/about' component={About}/>
              </div>
            </main>
          </div>
        </HashRouter>);
  }
}
export default App;
