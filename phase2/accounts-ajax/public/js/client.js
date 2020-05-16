/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Turns on strict mode so that the browser will be less forgiving 
 * of sloppy JavaScript. Will get warnings in the browser console 
 * if we do something wrong.
 */
"use strict";
 
let module = angular.module('AccountModule', ['ngResource']);
let uri = "http://localhost:9000/api";
 
module.factory('AccountService', function($resource) {
    return $resource(uri + "/account");
});
 
module.controller('AccountController', function(AccountService) {
   this.createAccount = function(account) {
       AccountService.save({}, account);
       console.log(account);
   };
})


