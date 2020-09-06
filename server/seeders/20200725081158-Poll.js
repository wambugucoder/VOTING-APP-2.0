'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.bulkInsert('Polls', [
      {
   question:"Is the app working",
   userId:"1",
   voters:["98"],
   active:true,
   createdAt: new Date(),
   updatedAt: new Date()
  
   },
 
], {});
  
  },

  down: async (queryInterface, Sequelize) => {
    await queryInterface.bulkDelete('Polls', null, {});
     
  }
};
