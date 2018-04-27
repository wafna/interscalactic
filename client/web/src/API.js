import {CHECK, orElse} from './Util';
/**
 * API client.
 * @param baseURL
 * @param opts For default exception handlers, {defaultOnFailure, defaultOnError}.
 * @returns {*} The API.
 */
export function API(baseURL, opts) {
  CHECK.PURE(this);
  let defOpts = orElse(opts, {});
  let defaultOnError = orElse(defOpts['defaultOnError'], function (error) {
    console.error('FETCH ERROR', error);
    // alert('FETCH ERROR ' + JSON.stringify(error));
  });
  let defaultOnFailure = orElse(defOpts['defaultOnFailure'], defaultOnError);
  const url = function (path, params) {
    let u = new URL(baseURL + path);
    Object.keys(orElse(params, {})).forEach(key => u.searchParams.append(key, params[key]));
    return u;
  };
  // handles promise from fetch by providing functional hooks to handle success and failure.
  const call = function (uri, opts) {
    let promise = fetch(uri, opts);
    return (onSuccess, exceptionHandlers) => {
      let hs = orElse(exceptionHandlers, {});
      promise.then(response => {
        // take anything that looks like success, verb notwithstanding.
        if (![200, 201].includes(response.status)) {
          orElse(hs.onFailure, defaultOnFailure)(response);
        } else {
          response.json().then(data => {
            onSuccess(data)
          });
        }
      }).catch(orElse(hs.onError, defaultOnError));
    };
  };
  let mode = 'cors';
  // Makes fetch endpoints for the standard REST verbs.
  // Verb!  That's what's happening.
  let verbs = path => {
    return {
      GET: params => call(url(path, params), {mode: mode, method: 'GET'}),
      POST: content => call(url(path), {
        mode: mode,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(content)
      }),
      DELETE: params => call(url(path, params), {mode: mode, method: 'DELETE'}),
      PUT: content => call(url(path), {
        mode: mode,
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(content)
      })
    };
  };
  let USERS = verbs('users');
  // let ROLES = verbs('roles');
  // let USERS_ROLES = verbs('users_roles');
  return {
    users: {
      list: () =>
          USERS.GET(),
      createUser: (user) =>
          USERS.POST({type: 'Create', givenName: user.givenName, familyName: user.familyName}),
      deleteUser: id =>
          USERS.POST({type: 'Delete', id: id}),
      updateUser: (user) =>
          USERS.POST({type: 'Update', id: user.id, givenName: user.givenName, familyName: user.familyName})
    }
  };
}
export const api = API('http://localhost:8080/api/');
