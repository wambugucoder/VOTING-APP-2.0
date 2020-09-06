'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.bulkInsert('Choices', [
      {
   option:"bad",
   pollId:"1",
   createdAt: new Date(),
   updatedAt: new Date()
   },
   {
    option:"good",
    pollId:"1",
    createdAt: new Date(),
    updatedAt: new Date()
    },
], {});
  
  },

  down: async (queryInterface, Sequelize) => {
    await queryInterface.bulkDelete('Choices', null, {});
     
  }
};
