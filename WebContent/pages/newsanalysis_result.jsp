<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="true" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>Insert title here</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,minimum-scale=1,user-scalable=no"/>
	<meta name="apple-mobile-web-app-capable" content="yes" />
	
	<link rel="stylesheet" href="css/style.css" />
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
	<!-- 
	<script type="text/javascript" src="js/util.js"></script>
	<script type="text/javascript" src="js/index.js"></script>
	-->
	<script type="text/javascript">
	var alchemyResults = ${alchemyResults};
	var cloudantResults = ${cloudantResults};
	
	var divOut = document.getElementById("out");
	divOut.innerHTML += "<br><br>AlchemyResults:<br>";
	divOut.innerHTML += JSON.stringify(alchemyResults);
	divOut.innerHTML += "<br><br>CloudantResults:<br>";
	divOut.innerHTML += JSON.stringify(cloudantResults);
	</script>
</body>
</html>