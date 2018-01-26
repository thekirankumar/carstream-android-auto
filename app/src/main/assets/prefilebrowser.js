var oldHead = document.head.innerHTML;document.head.innerHTML = "";

document.body.innerHTML = '<div id="listingParsingErrorBox">Oh no! This server is sending data that Google Chrome can\'t understand. Please <a href="http://code.google.com/p/chromium/issues/entry">report a bug</a> and include the <a href="LOCATION">raw listing</a>.</div>
<h1 id="header">Index of LOCATION</h1>
<div id="parentDirLinkBox" style="display:none">
    <a id="parentDirLink" class="icon up"><span id="parentDirText">[parent directory]</span></a>
</div>
<table>
    <thead>
        <tr class="header" id="theader">
            <th onclick="javascript:sortTable(0);">Name</th>
            <th class="detailsColumn" onclick="javascript:sortTable(1);">Size </th>
            <th class="detailsColumn" onclick="javascript:sortTable(2);">Date Modified </th>
        </tr>
    </thead>
    <tbody id="tbody"> </tbody>
</table>';

document.body.innerHTML+= oldHead;