<html>
	<body>
	<title>Login</title>
	<link rel="stylesheet" type="text/css" href="css/login.css">
		<form id = "login" action = "login.do" method = "post">
			<h1>Login</h1>
			<fieldset id = "inputs">
				<input id = "username" name = "username" type = "text" placeholder = "Username" autofocus required>
				<input id = "password" name = "password" type = "password" placeholder = "password" required>
			</fieldset>
			
			<fieldset id = "actions">
				<input type = "submit" id = "submit" value = "Login">
				<a href = "verifyReset.jsp">Forgot Your Password?</a><a href = "register.jsp">Register</a>
			</fieldset>		
		</form>
	</body>
</html>