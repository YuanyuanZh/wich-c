#include <stdio.h>
#include "wich.h"

int main(int ____c, char *____v[])
{
	setup_error_handlers();
	int x;
	x = 10;
	while ((x > 0)) {
	    printf("%1.2f\n", (x + 1.0));
	    x = (x - 1);
	}
	return 0;
}

