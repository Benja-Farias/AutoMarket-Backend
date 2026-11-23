# Respaldo y restauración de MongoDB

Los archivos físicos de MongoDB no se copian en el ZIP. Los datos residen en el servidor de MongoDB de EC2.

Instalar MongoDB Database Tools si vas a utilizar estos comandos:

```bash
mongodump --uri="mongodb://127.0.0.1:27017/automarket_fullstack" --out=./respaldo
mongorestore --uri="mongodb://127.0.0.1:27017" --nsInclude="automarket_fullstack.*" ./respaldo
```

**No subir respaldos con usuarios, hashes de contraseñas ni sesiones a repositorios públicos.** La semilla del código es suficiente para generar vehículos ficticios de ejemplo.
