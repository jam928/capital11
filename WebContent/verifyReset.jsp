<!DOCTYPE html>
<html>
	<body>
		<head>
			<title>Enter Username</title>
			<link type = "text/css" rel = "stylesheet" href = "css/main.css">
			<link type = "text/css" rel = "stylesheet" href = "css/verifyReset.css">
			
		</head>
		
		<div id = "wrapper">
			<div id = "header">
				<h2>Capital 11 Bank</h2>
			</div>
		</div>
		
		<div id = "container">
			<h3>Reset Username/Password</h3>
			
			<form action = "verify.do" method = "post">
			
				<table>
					<tbody>
						<tr>
							<td><label> Username: </label></td>
							<td><input type = "text" name = "username" /></td>
						</tr>
						<tr>
							<td><label></label></td>
							<td><input type = "submit" value = "Verify" class = "verify" /></td>
					</tbody>
				</table>
			</form>
			
			<p>
				<a href = "login.jsp">Back to the Login page</a>
			</p>
		</div>
	</body>
</html>