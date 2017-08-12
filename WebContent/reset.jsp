<!DOCTYPE html>
<html>
	<body>
		<head>
			<title>Reset Password</title>
			<link type = "text/css" rel = "stylesheet" href = "css/main.css">
			<link type = "text/css" rel = "stylesheet" href = "css/reset.css">
		</head>
		
		<div id = "wrapper">
			<div id = "header">
				<h2>Capital 11 Bank</h2>
			</div>
		</div>
		
		<div id = "container">
			<h3>Reset Username/Password</h3>
			
			<form action = reset.do method = "post">
				<input type = "hidden" name = "cid" value = "${user.cid}"/>
				
				<table>
					<tbody>
						<tr>
							<td><label>Password: </label>
							<td><input type = "password" name = "password" value = "${user.password}" /></td>
						</tr>
						<tr>
							<td><label></label></td>
							<td><input type = "submit" value = "Update" class = "update" /></td>
					</tbody>
				</table>
			</form>
			
			<p>
				<a href = "login.jsp">Back to the login page</a>
			</p>
		</div>
	</body>
</html>