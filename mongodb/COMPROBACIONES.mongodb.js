use('automarket_fullstack');
db.getCollectionNames();
db.vehiculos.find({}, {marca:1, modelo:1, precio:1}).limit(10);
db.usuarios.find({}, {email:1, rol:1, passwordHash:0});
db.consultas.find().limit(10);
