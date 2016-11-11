var body = d3.select("body");    

var formatDate = d3.time.format("%Y-%m-%d");
var margin = {top: 20, right: 20, bottom: 30, left: 50},
  width = 600 - margin.left - margin.right,
  height = 400 - margin.top - margin.bottom;
var x = d3.time.scale().range([0, width]);
var y = d3.scale.linear().range([height, 0]);
var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom");
var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left");
var line = d3.svg.line()
    .x(function(d) { return x(d.publicationDate); })
    .y(function(d) { return y(d.sentimentScore); });
    
var svg = body.append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
data.forEach(function(d){
  d.publicationDate = formatDate.parse(d.publicationDate);
  d.sentimentScore = parseFloat(d.sentimentScore);
});
x.domain(d3.extent(data, function(d) { 
  return d.publicationDate; 
}));
y.domain(d3.extent(data, function(d) { return d.sentimentScore; }));

svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + height + ")")
    .call(xAxis);
svg.append("g")
    .attr("class", "y axis")
    .call(yAxis)
  .append("text")
    .attr("transform", "rotate(-90)")
    .attr("y", 6)
    .attr("dy", ".71em")
    .style("text-anchor", "end")
    .text("Sentiment");
svg.append("path")
    .datum(data)
    .attr("class", "line")
    .attr("d", line);
   
    function tabulate(data, columns) {
      var table = d3.select('body').append('table')
  var thead = table.append('thead')
  var tbody = table.append('tbody');
  // append the header row
  thead.append('tr')
    .selectAll('th')
    .data(columns).enter()
    .append('th')
      .text(function (column) { return column; });
  // create a row for each object in the data
  var rows = tbody.selectAll('tr')
    .data(data)
    .enter()
    .append('tr');
  // create a cell in each row for each column
  var cells = rows.selectAll('td')
    .data(function (row) {
      return columns.map(function (column) {
        val = row[column];
        if(row[column] instanceof Date){
          val = row[column].getFullYear()+"-"+
            (row[column].getMonth()+1)+"-"+row[column].getDate();
        }
        return {column: column, value: val};
      });
    })
    .enter()
    .append('td')
      .text(function (d) { return d.value; });
  return table;
}
var table = tabulate(data, ['publicationDate', 'sentimentScore']);