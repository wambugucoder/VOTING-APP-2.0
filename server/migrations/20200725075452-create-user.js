'use strict';
module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.createTable('Users', {
      id: {
        allowNull: false,
        autoIncrement: true,
        primaryKey: true,
        type: Sequelize.INTEGER
      },
      username: {
        type: Sequelize.STRING,
        allowNull: false,
      },
      email: {
        type: Sequelize.STRING,
        unique:true,
        allowNull: false,
      },
      password: {
        type: Sequelize.STRING,
        allowNull: false,
        
      },
      verified:{
        type: Sequelize.BOOLEAN,
          defaultValue:false,
          allowNull: false,
 
      },
      temporarytoken:{
        type:Sequelize.STRING,
        allowNull: false,
      },
      role: {
        type: Sequelize.ENUM,
        values:["user","admin"],
        defaultValue:"user"
      },
      createdAt: {
        allowNull: false,
        type: Sequelize.DATE,
        default:Date.now()
      },
      updatedAt: {
        allowNull: false,
        type: Sequelize.DATE,
        default:Date.now()
      }
    });
  },
  down: async (queryInterface, Sequelize) => {
    await queryInterface.dropTable('Users');
  }
};