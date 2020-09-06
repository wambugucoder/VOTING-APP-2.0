'use strict';
module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.createTable('Polls', {
      id: {
        allowNull: false,
        autoIncrement: true,
        primaryKey: true,
        type: Sequelize.INTEGER
      },
      question: {
        type: Sequelize.STRING,
        allowNull: false,
      },
      voters: {
        type: Sequelize.ARRAY(Sequelize.STRING),
        defaultValue:[]
      },
      userId: {
        type: Sequelize.INTEGER,
        allowNull: false,
        onDelete:'CASCADE',
        onUpdate:"CASCADE",
        references:{
          model:'Users',
          key:'id',
         
        }
      },
      active: {
        type: Sequelize.BOOLEAN,
        defaultValue:true,
        allowNull: false,
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
    await queryInterface.dropTable('Polls');
  }
};