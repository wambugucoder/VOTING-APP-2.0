'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
   
      await queryInterface.bulkInsert('Users', [
        {
      username:"admin@",
      email:"admin@admin.com",
      password:"123Jos",
      verified:true,
      temporarytoken:"empty",
      role:"admin",
      createdAt: new Date(),
      updatedAt: new Date()
      
     },
    {
      username:"user1@",
      email:"user@user.com",
      password:"123Jos",
      verified:true,
      temporarytoken:"empty",
      role:"user",
      createdAt: new Date(),
      updatedAt: new Date()
      
    }
  ], {});
    
  },

  down: async (queryInterface, Sequelize) => {
   
      await queryInterface.bulkDelete('Users', null, {});
     
  }
};
