<%@ page import="web.Dot" %>
<%@ page import="java.text.DecimalFormat" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!doctype html>
<html lang=ru>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <meta name="description" content="если честно, я до сих пор не знаю, зачем этот сайт, мне такой вариант попался">
    <link rel="stylesheet" href="stylesheets/style.css">
    <link rel="icon" href="image/favicon.ico" />
    <title>Веб-прога - Лаба 2 - результат</title>
</head>
<body>
<%
    Dot dot = (Dot) request.getAttribute("dot");
%>
<header>
    <span>Васильев Никита Алексеевич</span>
    <span>P3208</span>
    <span>Вариант №678917</span>
</header>
<main>
    <% if (dot != null) {
        DecimalFormat df = new DecimalFormat("#.###");
    %>
    <table class="resultTable" id="currentResult">
        <thead>
        <tr>
            <th>X</th>
            <th>Y</th>
            <th>R</th>
            <th>Время выполнения (нс)</th>
            <th>Результат</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><%= df.format(dot.x()) %></td>
            <td><%= df.format(dot.y()) %></td>
            <td><%= dot.r() %></td>
            <td><%= dot.duration() %> нс</td>
            <td><%= dot.check() ? "Попадание" : "Не попадание" %></td>
        </tr>
        </tbody>
    </table>
    <% } else { %>
    <p>Нет данных для отображения текущего результата.</p>
    <% } %>
    <a href="index.jsp">
        <button class="defaultButton" id="returnButton">Вернуться на главную</button>
    </a>
</main>
<script type="text/javascript" src="script.js"></script>
</body>