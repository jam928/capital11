<!DOCTYPE html>
<html>
	<body>
		<head>
			<title>Deposit</title>
			<link type = "text/css" rel = "stylesheet" href = "css/main.css">
			<link type = "text/css" rel = "stylesheet" href = "css/update.css">
		</head>
		
		<div id = "wrapper">
			<div id = "header">
				<h2>Capital 11 Bank</h2>
			</div>
		</div>
		
		<div id = "container">
			<h3>Update User Info</h3>
			
			<form action = "update.do" method = "post">
			
				<table>
					<tbody>
						<tr>
							<td><label>Email: </label>
							<td><input type = "email" name = "email" value = "${currentUser.email}" /></td>
						</tr>
						<tr>
							<td><label>Username: </label>
							<td><input type = "text" name = "username" value = "${currentUser.username}" /></td>
						</tr>
						<tr>
							<td><label>Password: </label>
							<td><input type = "password" name = "password" value = "${currentUser.username}" /></td>
						</tr>
						<tr>
							<td><label></label></td>
							<td><input type = "submit" value = "Update" class = "update" /></td>
					</tbody>
				</table>
			</form>
			
			<p>
				<a href = "default.do">Back to the main page</a>
			</p>
		</div>
	</body>
</html>