import React from 'react';
import './App.css';
import * as W from './Widgets';
import * as U from './Util';
import {API} from './API';
import {Reactor} from './Reactor';
// All this to avoid the lint of having a var declaration for the index. :/
class DummyNames {
  constructor() {
    this.index = 0;
  }
  static dummy_names() {
    return [['Commandant', 'Steele'], ['General', 'Knox'], ['Taylor', 'Cobb'], ['Jaynis', 'Cobb']];
  }
  get() {
    const name = DummyNames.dummy_names()[this.index];
    this.index = (1 + this.index) % DummyNames.dummy_names().length;
    return name;
  }
}
const dummyNames = new DummyNames();
class CreateUser extends Reactor {
  constructor(props) {
    super(props);
    this.state = U.orElse(props.user, {id: 0, givenName: '', familyName: ''});
    this._givenName = super.lens('givenName');
    this._familyName = super.lens('familyName');
  }
  render() {
    let givenName = this._givenName.take();
    let familyName = this._familyName.take();
    let enabled = !!givenName && !!familyName;
    return (
        <div className="well">
          <form>
            <div><label> Given Name: <W.Input lens={this._givenName}/> </label></div>
            <div><label> Family Name: <W.Input lens={this._familyName}/> </label></div>
            <div><W.Button className="btn btn-primary" disabled={!enabled} onClick={() => {
              if (this.state.id)
                this.props.updateUser(this.state.id, this._givenName.take(), this._familyName.take());
              else
                this.props.createUser(this._givenName.take(), this._familyName.take());
            }}>{this.state.id ? 'Update' : 'Create'}</W.Button>
              <W.Button className="btn btn-secondary" onClick={() => {
                const name = dummyNames.get();
                this._givenName.put(name[0]);
                this._familyName.put(name[1]);
              }}>Make something up</W.Button>
              <W.Button className="btn btn-warning" onClick={() => {
                this._givenName.put('');
                this._familyName.put('');
              }}>Clear</W.Button>
            </div>
          </form>
        </div>
    );
  }
}
const api = API("http://localhost:8080/api/");
class App extends Reactor {
  constructor(props) {
    super(props);
    this.state = {users: null, edit: null};
    this._users = super.lens('users');
    this._edit = super.lens('edit');
    this.updateUsers = this.updateUsers.bind(this);
  }
  updateUsers() {
    api.users.list()(this._users.put);
  }
  componentDidMount() {
    this.updateUsers();
  }
  render() {
    return (
        <main role="main">
          <div className="jumbotron">
            <div className="container">
              <h1 className="display-3">InterScalactic</h1>
              <p>Soup to nuts with React, Akka, Slick.</p>
            </div>
          </div>
          <div className="container">
            <CreateUser user={this._edit.take()} createUser={(givenName, familyName) => {
              api.users.createUser(givenName, familyName)(() => {
                this.updateUsers();
              });
            }} updateUser={(id, givenName, familyName) => {
              api.users.updateUser(id, givenName, familyName)(() => {
                this.updateUsers();
              });
            }}/>
            {(this.state.users) ? (
                <div>{this.state.users.map(user => {
                      return <div key={user.id}>
                        <W.Button onClick={() => {
                          api.users.deleteUser(user.id)(r => {
                            this.updateUsers();
                          });
                        }}>X</W.Button><span>{' [' + user.id + '] ' + user.givenName + ' ' + user.familyName}</span>
                      </div>
                    }
                )}</div>
            ) : null}
          </div>
        </main>
    );
  }
}
export default App;
