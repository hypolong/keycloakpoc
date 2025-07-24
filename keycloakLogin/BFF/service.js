const express = require('express');
const app = express();
const port = 3010;
 
app.get('/api/data', (req, res) => {
  // 这里可以根据前端请求定制数据
  const data = { message: "Hello from BFF!", userId: req.query.userId };
  res.json(data);
});
 
app.listen(port, () => {
  console.log(`BFF service running at http://localhost:${port}`);
});