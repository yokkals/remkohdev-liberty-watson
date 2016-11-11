<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>Bluemix Workshop - News Analyzer</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,minimum-scale=1,user-scalable=no"/>
	<meta name="apple-mobile-web-app-capable" content="yes" />
	
	<link rel="stylesheet" href="css/style.css" />
	<link rel="stylesheet" href="css/d3.css" />
</head>

<body>
	<div>
		<header>
			<h1>Bluemix Workshop</h1>
		</header>	
		<section id='appinfo'>
			<h1>News Analyzer</h1>
			<div id="out"></div>
		</section>
		<footer>
			<div id="errorDiv" class='errorMsg'></div>	
		</footer>
	</div>
	
	<script type=text/javascript src="js/jquery/jquery-3.1.1.min.js"></script> 
    <script type=text/javascript src="js/d3js/d3-v3/d3.min.js"></script>
  
    <script type="text/javascript"> 
	var response = ${result}[0];
	var responseAsStr = JSON.stringify(response);
    
    var data = response.result;
    var respStartDate = response.startdate;
    var respEndDate = response.enddate;
    var respSearchTerm = response.searchterm;
    
  	
    var body1 = d3.select("body");    
    body1.append("h1")
      .text("News Sentiment for '"+respSearchTerm+"'");
    body1.append("h3")
      .text("from "+respStartDate+" to "+respEndDate);
    
	</script>
	
	<script type=text/javascript src="js/d3jsgraph.js"></script>

</body>
</html>