

```bash
mongodump --uri="mongodb://127.0.0.1:27017/automarket_fullstack" --out=./respaldo
mongorestore --uri="mongodb://127.0.0.1:27017" --nsInclude="automarket_fullstack.*" ./respaldo
```

