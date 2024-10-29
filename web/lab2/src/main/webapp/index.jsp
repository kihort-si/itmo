<%@ page import="java.util.List" %>
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
    <title>Веб-прога - Лаба 2</title>
</head>
<body>

<%
    List<Dot> dots = (List<Dot>) session.getAttribute("dots");
%>

<header>
    <span>Васильев Никита Алексеевич</span>
    <span>P3208</span>
    <span>Вариант №678917</span>
</header>
<main>
    <form id="mainForm" method="post" action="${pageContext.request.contextPath}/controller" onsubmit="return validate()">
        <table class="inputTable">
            <thead>
            <tr>
                <th colspan="3">Введите данные для проверки</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <span>Укажите X:</span>
                    <div class="xButtons">
                        <input value="-4" type="button">
                        <input value="-3" type="button">
                        <input value="-2" type="button">
                        <input value="-1" type="button">
                        <input value="0" type="button">
                        <input value="1" type="button">
                        <input value="2" type="button">
                        <input value="3" type="button">
                        <input value="4" type="button">
                    </div>
                    <span class="errorMessage" id="xError">a</span>
                    <input type="hidden" id="xInput" name="x" value="">
                </td>
                <td>
                    <span>Укажите Y:</span>
                    <div class="yText">
                        <input id="yInput" type="text" name="y" placeholder="от -5 до 5">
                    </div>
                    <span class="errorMessage" id="yError">w</span>
                </td>
            </tr>
            <tr>
                <td>
                    <span>Укажите R:</span>
                    <div class="rButtons">
                        <input value="1" type="button">
                        <input value="2" type="button">
                        <input value="3" type="button">
                        <input value="4" type="button">
                        <input value="5" type="button">
                    </div>
                    <span class="errorMessage" id="rError">f</span>
                    <input type="hidden" id="rInput" name="r" value="">
                </td>
                <td>
                </td>
            </tr>
            </tbody>
        </table>
        <button class="defaultButton" id="submitButton" type="submit">
            Проверить
        </button>
    </form>
    <div class="graphArea">
        <div id="graphContainer">
            <svg id="circles" fill="none" xmlns="http://www.w3.org/2000/svg"></svg>
            <object type="image/svg+xml" data="image/coordinates.svg" class="coordinateAxes"></object>
            <svg id="graph" width="400" height="400" viewBox="0 0 400 400" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd" clip-rule="evenodd" d="M200 364C290.575 364 364 290.575 364 200H200V364Z" fill="#000035" fill-opacity="0.5"/>
                <path d="M364 200H365V199H364V200ZM200 364H199V365H200V364ZM200 200V199H199V200H200ZM363 200C363 290.022 290.022 363 200 363V365C291.127 365 365 291.127 365 200H363ZM200 201H364V199H200V201ZM201 364V200H199V364H201Z" fill="black" mask="url(#path-1-outside-1_14_12)"/>
                <rect x="35.5" y="117.5" width="165" height="83" fill="#000035" fill-opacity="0.5" stroke="black"/>
                <path d="M282 200.5H283.207L282.354 199.646L200.354 117.646L199.5 116.793V118V200V200.5H200H282Z" fill="#000035" fill-opacity="0.5" stroke="black"/>
            </svg>


        </div>
    </div>
    <table class="resultTable" id="result">
        <thead>
        <tr>
            <th>X</th>
            <th>Y</th>
            <th>R</th>
            <th>Текущее время</th>
            <th>Время выполнения (нс)</th>
            <th>Результат</th>
        </tr>
        </thead>
        <tbody>
        <% if (dots != null) {
            DecimalFormat df = new DecimalFormat("#.###");
            for (Dot dot : dots) {
        %>
        <tr>
            <td><%= df.format(dot.x()) %></td>
            <td><%= df.format(dot.y()) %></td>
            <td><%= dot.r() %></td>
            <td><%= dot.date() %></td>
            <td><%= dot.duration() %> нс</td>
            <td><%= dot.check() ? "Попадание" : "Не попадание" %></td>
        </tr>
        <%
            }
        } else {
        %>
        <tr>
            <td colspan="6">Нет данных</td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% if (dots != null) { %>
    <form action="${pageContext.request.contextPath}/clearHistory" method="post">
        <button class="defaultButton" id="deleteButton" type="submit">Очистить историю</button>
    </form>
    <% } %>
</main>
<footer>
    <div>
        <span>Copyright. All rights reserved. 2024</span>
        <a href="https://github.com/kihort-si/web-lab2"><img id="githubLogo" src="image/github.svg" alt="Исходный код на GitHub"></a>
    </div>
    <hr>
</footer>
<script type="text/javascript" src="script.js"></script>
<%
    if (dots != null) {
        for (Dot currentDot : dots) {
            String color = currentDot.check() ? "green" : "red";
%>
<script>
    placeDot(<%= currentDot.x() %>, <%= currentDot.y() %>, "<%= color %>");
</script>
<%
        }
    }
%>

</body>
</html>