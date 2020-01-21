.386
.model flat, stdcall
.stack 4096
ExitProcess PROTO, dwExitCode: DWORD
.data
	i2 dd ?
	c2 dd ?
	a2 dd ?

.code

foo PROC
	mov eax, 0
	mov i2, eax
.WHILE (i2 < 3)
	mov eax, DWORD PTR[ESP + 4]
	mov c2, eax
	add eax, i2
	mov c2, eax
	INC i2

.ENDW

	mov eax, c2
	mov c2, eax
	add eax, DWORD PTR[ESP + 8]
	mov c2, eax
	mov ebx, c2
	ret
foo ENDP

foo2 PROC
.IF (DWORD PTR[ESP + 4] == 10)
	DEC DWORD PTR[ESP + 4]

.ELSE
	INC DWORD PTR[ESP + 4]

.ENDIF

	ret
foo2 ENDP

main PROC
	mov eax, 41h
	mov a2, eax
	push 5
	call foo2
	pop ecx
	push 3
	push 2
	call foo
	pop ecx
	pop ecx
	mov ebx, 0
invoke ExitProcess, 0
main ENDP

END main
