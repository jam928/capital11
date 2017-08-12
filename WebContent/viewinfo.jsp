<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<!DOCTYPE html>
<html>

<head>
	<title>Main</title>
	<link type = "text/css" rel = "stylesheet" href = "css/main.css">
</head>

<body>

	<div id = "wrapper">
		<div id = "header">
			<h2>Capital 11 Bank</h2>
		</div>
	</div>

	<br><br>
	Name: ${currentUser.name} <br><br>
	Email: ${currentUser.email}<br><br>
	Username: ${currentUser.username}<br><br>
	BirthDay: ${currentUser.birthday}<br><br>
	Gender: ${currentUser.gender}<br><br>
	
	<p>
		<a href = "update.jsp">Update Info</a>
		<br>
		<a href = "default.do">Back to the Main Page</a>
	</p>
</body>
</html>