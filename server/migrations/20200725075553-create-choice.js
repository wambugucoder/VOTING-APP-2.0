'use strict';
module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.createTable('Choices', {
      id: {
        allowNull: false,
        autoIncrement: true,
        primaryKey: true,
        type: Sequelize.INTEGER
      },
      option: {
        type: Sequelize.STRING,
        allowNull: false,
      },
      votes: {
        type: Sequelize.INTEGER,
        defaultValue:0
      },
      pollId: {
        type: Sequelize.INTEGER,
        allowNull: false,
        onDelete:'CASCADE',
        onUpdate:"CASCADE",
        references:{
          model:'Polls',
          key:'id',
          
        }
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
    await queryInterface.dropTable('Choices');
  }
};